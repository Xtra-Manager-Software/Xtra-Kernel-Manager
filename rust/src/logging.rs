use std::collections::VecDeque;
use std::sync::OnceLock;
use std::sync::atomic::{AtomicU8, Ordering};
use std::time::SystemTime;

use parking_lot::Mutex;
use serde::Serialize;
use tracing::field::Visit;
use tracing::subscriber::Subscriber;
use tracing::{Event, Id, Level, Metadata, span};

/// Maximum entries retained in the in-memory diagnostic ring-buffer.
const MAX_RING_BUFFER_ENTRIES: usize = 128;

#[derive(Debug, Clone, Serialize)]
pub struct LogEntry {
    pub timestamp_ms: u64,
    pub level: &'static str,
    pub target: String,
    pub message: String,
}

struct LogRingBuffer {
    entries: VecDeque<LogEntry>,
}

static RING_BUFFER: OnceLock<Mutex<LogRingBuffer>> = OnceLock::new();
static MAX_LEVEL: AtomicU8 = AtomicU8::new(LevelFilter::Debug as u8);

#[derive(Debug, Clone, Copy, PartialEq, Eq)]
#[repr(u8)]
pub enum LevelFilter {
    Off = 0,
    Error = 1,
    Warn = 2,
    Info = 3,
    Debug = 4,
    Trace = 5,
}

impl LevelFilter {
    pub fn from_u8(v: u8) -> Self {
        match v {
            0 => Self::Off,
            1 => Self::Error,
            2 => Self::Warn,
            3 => Self::Info,
            4 => Self::Debug,
            _ => Self::Trace,
        }
    }
}

#[cfg(target_os = "android")]
unsafe extern "C" {
    fn __android_log_write(
        prio: std::ffi::c_int,
        tag: *const std::ffi::c_char,
        text: *const std::ffi::c_char,
    ) -> std::ffi::c_int;
}

#[cfg(target_os = "android")]
fn dispatch_logcat(level: &Level, _target: &str, message: &str) {
    let prio = match *level {
        Level::ERROR => 6, // ANDROID_LOG_ERROR
        Level::WARN => 5,  // ANDROID_LOG_WARN
        Level::INFO => 4,  // ANDROID_LOG_INFO
        Level::DEBUG => 3, // ANDROID_LOG_DEBUG
        Level::TRACE => 2, // ANDROID_LOG_VERBOSE
    };

    let tag = c"XKM_Native";

    if let Ok(c_msg) = std::ffi::CString::new(message) {
        // SAFETY: `tag` is a valid static C-string literal null-terminated,
        // and `c_msg` is verified free of interior NUL bytes.
        unsafe {
            __android_log_write(prio, tag.as_ptr(), c_msg.as_ptr());
        }
    }
}

#[cfg(not(target_os = "android"))]
fn dispatch_host(level: &Level, target: &str, message: &str) {
    let level_str = match *level {
        Level::ERROR => "\x1b[31mERROR\x1b[0m",
        Level::WARN => "\x1b[33m WARN\x1b[0m",
        Level::INFO => "\x1b[32m INFO\x1b[0m",
        Level::DEBUG => "\x1b[36mDEBUG\x1b[0m",
        Level::TRACE => "\x1b[35mTRACE\x1b[0m",
    };
    eprintln!("[{}] {}: {}", level_str, target, message);
}

#[derive(Default)]
struct StringVisitor {
    message: String,
}

impl StringVisitor {
    fn finish(self) -> String {
        self.message
    }
}

impl Visit for StringVisitor {
    fn record_debug(&mut self, field: &tracing::field::Field, value: &dyn std::fmt::Debug) {
        use std::fmt::Write;
        if field.name() == "message" {
            let _ = write!(self.message, "{:?}", value);
        } else {
            let _ = write!(self.message, " {}={:?}", field.name(), value);
        }
    }

    fn record_str(&mut self, field: &tracing::field::Field, value: &str) {
        if field.name() == "message" {
            self.message.push_str(value);
        } else {
            use std::fmt::Write;
            let _ = write!(self.message, " {}={}", field.name(), value);
        }
    }
}

struct XkmSubscriber;

impl Subscriber for XkmSubscriber {
    fn enabled(&self, metadata: &Metadata<'_>) -> bool {
        let max = MAX_LEVEL.load(Ordering::Relaxed);
        let level = metadata.level();
        let level_u8 = match *level {
            Level::ERROR => 1,
            Level::WARN => 2,
            Level::INFO => 3,
            Level::DEBUG => 4,
            Level::TRACE => 5,
        };
        level_u8 <= max
    }

    fn new_span(&self, _span: &span::Attributes<'_>) -> Id {
        Id::from_u64(1)
    }

    fn record(&self, _span: &Id, _values: &span::Record<'_>) {}
    fn record_follows_from(&self, _span: &Id, _follows: &Id) {}

    fn event(&self, event: &Event<'_>) {
        if !self.enabled(event.metadata()) {
            return;
        }

        let mut visitor = StringVisitor::default();
        event.record(&mut visitor);

        let target = event.metadata().target();
        let level = event.metadata().level();
        let message = visitor.finish();

        // 1. Dispatch to platform logging sink
        #[cfg(target_os = "android")]
        dispatch_logcat(level, target, &message);

        #[cfg(not(target_os = "android"))]
        dispatch_host(level, target, &message);

        // 2. Append to diagnostic in-memory ring-buffer
        if let Some(buf) = RING_BUFFER.get() {
            let mut guard = buf.lock();
            if guard.entries.len() >= MAX_RING_BUFFER_ENTRIES {
                guard.entries.pop_front();
            }
            let level_str = match *level {
                Level::ERROR => "ERROR",
                Level::WARN => "WARN",
                Level::INFO => "INFO",
                Level::DEBUG => "DEBUG",
                Level::TRACE => "TRACE",
            };
            let ts = SystemTime::now()
                .duration_since(SystemTime::UNIX_EPOCH)
                .map(|d| d.as_millis() as u64)
                .unwrap_or(0);

            guard.entries.push_back(LogEntry {
                timestamp_ms: ts,
                level: level_str,
                target: target.to_string(),
                message,
            });
        }
    }

    fn enter(&self, _span: &Id) {}
    fn exit(&self, _span: &Id) {}
}

/// Initialize native logging.
/// Should be called during `JNI_OnLoad` or application startup.
pub fn init() {
    RING_BUFFER.get_or_init(|| {
        Mutex::new(LogRingBuffer {
            entries: VecDeque::with_capacity(MAX_RING_BUFFER_ENTRIES),
        })
    });

    let _ = tracing::subscriber::set_global_default(XkmSubscriber);
}

/// Update dynamic log level threshold at runtime.
pub fn set_max_level(level: LevelFilter) {
    MAX_LEVEL.store(level as u8, Ordering::Relaxed);
}

/// Retrieve recent log entries from the ring-buffer.
pub fn get_recent_logs(max_count: usize) -> Vec<LogEntry> {
    if let Some(buf) = RING_BUFFER.get() {
        let guard = buf.lock();
        let skip = guard.entries.len().saturating_sub(max_count);
        guard.entries.iter().skip(skip).cloned().collect()
    } else {
        Vec::new()
    }
}

/// Retrieve recent logs as a JSON string for easy Kotlin JNI consumption.
pub fn get_recent_logs_json(max_count: usize) -> String {
    let logs = get_recent_logs(max_count);
    serde_json::to_string(&logs).unwrap_or_else(|_| "[]".to_string())
}
