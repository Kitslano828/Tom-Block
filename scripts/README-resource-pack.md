# Publishing a new build-server resource pack

Edit files under `resource-pack/` in the TomBlock project. When ready, run this
from the TomBlock project directory in PowerShell:

```powershell
.\scripts\Publish-ResourcePack.ps1 -Tag pack-v2
```

Increase the version for each subsequent release (`pack-v3`, `pack-v4`, etc.).
The script copies `pack.mcmeta` and `assets/` to the separate public pack repo,
shows the staged changes, and asks you to type `PUBLISH`. It then commits and
pushes the new tag. GitHub Actions builds the versioned ZIP. The script waits
for that ZIP, verifies its structure, calculates its SHA-1, and updates only
the required-pack fields in the Ubuntu build server's `server.properties`.
It backs up the previous properties file there.

The script does **not** restart Paper or deploy the plugin JAR. When it reports
success, restart the build server at a convenient time:

```powershell
ssh -i "$env:USERPROFILE\.ssh\codex_tomblock_ubuntu" tom@tom-ROG-Strix-SCAR-18-G834JYR-G834JYR.local "sudo systemctl restart tomblock-build.service"
```

Because `sudo` may require an interactive password, you can also SSH into
Ubuntu first and run `sudo systemctl restart tomblock-build.service` there.
Then join with a client and check that the updated texture appears.

If a file was removed from `resource-pack/assets`, the script stops rather
than silently leaving the old public file in the ZIP. Remove that stale file
from the public pack repo after reviewing it, then rerun the script.
