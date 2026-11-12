use anyhow::{Context, Result, ensure};
use std::collections::BTreeMap;
use std::fs;
use std::path::Path;

fn sanitize_identifier(s: &str) -> bool {
    !s.is_empty()
        && s.len() <= 64
        && !s.contains('/')
        && !s.contains('\\')
        && !s.contains("..")
        && s.chars()
            .all(|c| c.is_ascii_alphanumeric() || c == '_' || c == '-')
}

/// Retrieve all runtime tunable parameters for a given kernel module.
pub fn get_module_parameters(module_name: &str) -> BTreeMap<String, String> {
    let mut params = BTreeMap::new();
    if !sanitize_identifier(module_name) {
        return params;
    }

    let dir_path = format!("/sys/module/{}/parameters", module_name);
    let Ok(entries) = fs::read_dir(dir_path) else {
        return params;
    };

    for entry in entries.flatten() {
        let file_name = entry.file_name();
        let name_str = file_name.to_string_lossy();
        if !sanitize_identifier(&name_str) {
            continue;
        }

        if let Ok(val) = fs::read_to_string(entry.path()) {
            params.insert(name_str.to_string(), val.trim().to_string());
        }
    }

    params
}

/// Update a specific kernel module parameter in sysfs.
pub fn set_module_parameter(module_name: &str, param_name: &str, value: &str) -> Result<()> {
    ensure!(
        sanitize_identifier(module_name),
        "Invalid module name: {}",
        module_name
    );
    ensure!(
        sanitize_identifier(param_name),
        "Invalid parameter name: {}",
        param_name
    );

    let path_str = format!("/sys/module/{}/parameters/{}", module_name, param_name);
    let path = Path::new(&path_str);
    ensure!(path.exists(), "Parameter does not exist: {}", path_str);

    fs::write(path, value.trim())
        .with_context(|| format!("Failed to write parameter: {}", path_str))?;
    Ok(())
}
