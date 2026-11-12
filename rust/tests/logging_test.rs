use tracing::{debug, error, info, trace, warn};
use xkm_native::logging::{self, LevelFilter};

#[test]
fn test_logging_ring_buffer_and_filtering() {
    logging::init();
    logging::set_max_level(LevelFilter::Trace);

    info!("Test info log message");
    warn!("Test warn log with value={}", 42);
    error!("Test error event");
    debug!("Test debug details");
    trace!("Test trace telemetry tick");

    let logs = logging::get_recent_logs(10);
    assert!(
        !logs.is_empty(),
        "Logs ring buffer should contain recorded events"
    );

    let last = logs.last().expect("Should have at least one entry");
    assert_eq!(last.level, "TRACE");
    assert!(last.message.contains("Test trace telemetry tick"));

    // Check JSON serialization
    let json = logging::get_recent_logs_json(5);
    assert!(json.starts_with('['));
    assert!(json.ends_with(']'));
    assert!(json.contains("Test trace telemetry tick"));

    // Test Level Filtering: set max to Warn (should ignore info, debug, trace)
    logging::set_max_level(LevelFilter::Warn);
    debug!("This debug should be filtered out");
    trace!("This trace should be filtered out");
    info!("This info should be filtered out");

    warn!("Active warning message");
    let logs_after = logging::get_recent_logs(5);
    let last_after = logs_after.last().expect("Should contain latest warning");
    assert_eq!(last_after.level, "WARN");
    assert!(last_after.message.contains("Active warning message"));
}
