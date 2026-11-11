use anyhow::Result;

#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum DndMode {
    All = 1,
    Priority = 2,
}

pub fn apply_dnd(mode: DndMode) -> Result<()> {
    #[cfg(target_os = "android")]
    {
        let arg = match mode {
            DndMode::All => "all",
            DndMode::Priority => "priority",
        };
        let _ = std::process::Command::new("cmd")
            .args(["notification", "set_dnd", arg])
            .status();

        let zen_val = match mode {
            DndMode::All => "0",
            DndMode::Priority => "1",
        };
        let _ = std::process::Command::new("settings")
            .args(["put", "global", "zen_mode", zen_val])
            .status();

        Ok(())
    }

    #[cfg(not(target_os = "android"))]
    {
        let _ = mode;
        Ok(())
    }
}
