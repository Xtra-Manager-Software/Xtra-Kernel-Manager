use std::time::Duration;

pub fn fmt_duration(d: Duration) -> String {
    let us = d.as_micros();
    if us < 1000 {
        format!("{} µs", us)
    } else {
        format!("{:.2} ms", us as f64 / 1000.0)
    }
}
