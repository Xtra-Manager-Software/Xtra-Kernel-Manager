use serde::Serialize;
use std::fs;
use std::path::Path;

#[derive(Debug, Clone, PartialEq, Eq, Serialize)]
pub struct KernelModuleInfo {
    pub name: String,
    pub size_bytes: u64,
    pub instances: i32,
    pub used_by: Vec<String>,
    pub state: String,
    pub taint: Option<String>,
    pub has_parameters: bool,
    pub is_protected: bool,
}

/// Protected modules that should never be unloaded to prevent kernel panic or root loss
const PROTECTED_MODULES: &[&str] = &[
    "kernelsu", "ksud", "magisk", "apatch", "kgsl", "adreno", "mali", "pvr",
];

pub fn is_module_protected(name: &str) -> bool {
    let lower = name.to_ascii_lowercase();
    PROTECTED_MODULES
        .iter()
        .any(|&p| lower == p || lower.contains(p))
}

pub fn parse_proc_modules(content: &str) -> Vec<KernelModuleInfo> {
    let mut modules = Vec::new();

    for line in content.lines() {
        let trimmed = line.trim();
        if trimmed.is_empty() {
            continue;
        }

        let mut parts = trimmed.split_whitespace();
        let Some(name) = parts.next() else { continue };
        let size_bytes = parts
            .next()
            .and_then(|s| s.parse::<u64>().ok())
            .unwrap_or(0);
        let instances = parts
            .next()
            .and_then(|s| s.parse::<i32>().ok())
            .unwrap_or(0);
        let used_by_str = parts.next().unwrap_or("-");
        let state = parts.next().unwrap_or("Live").to_string();

        let used_by: Vec<String> = if used_by_str == "-" {
            Vec::new()
        } else {
            used_by_str
                .split(',')
                .map(str::trim)
                .filter(|s| !s.is_empty() && *s != "-")
                .map(String::from)
                .collect()
        };

        // Query sysfs for metadata
        let sys_path = format!("/sys/module/{}", name);
        let taint = fs::read_to_string(format!("{}/taint", sys_path))
            .ok()
            .map(|s| s.trim().to_string())
            .filter(|s| !s.is_empty());

        let params_path = format!("{}/parameters", sys_path);
        let has_parameters = Path::new(&params_path).is_dir();

        let is_protected = is_module_protected(name);

        modules.push(KernelModuleInfo {
            name: name.to_string(),
            size_bytes,
            instances,
            used_by,
            state,
            taint,
            has_parameters,
            is_protected,
        });
    }

    modules
}

pub fn list_loaded_modules() -> Vec<KernelModuleInfo> {
    match fs::read_to_string("/proc/modules") {
        Ok(content) => parse_proc_modules(&content),
        Err(_) => Vec::new(),
    }
}
