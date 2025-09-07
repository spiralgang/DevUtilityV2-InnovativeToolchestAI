```markdown
# UserLAnd → Android (concise README)

Summary
- Use SFTP (preferred) or HTTP fallback to transfer a tarball from your UserLAnd filesystem to the Android device.
- Use a SAF-capable file manager (Solid Explorer, CX, Total Commander) to paste the files into:
  /storage/emulated/0/Android/data/tech.ula/files/storage

Quick steps
1. Save and chmod +x quick_start.sh in UserLAnd:
   mkdir -p ~/integrations/userland_transfer
   (paste quick_start.sh into ~/integrations/userland_transfer/)
   chmod +x ~/integrations/userland_transfer/quick_start.sh

2. Run:
   ~/integrations/userland_transfer/quick_start.sh /path/to/your/data 8000

3. On phone:
   - Open Solid Explorer → add SFTP connection point to 127.0.0.1 port 8022 (or 22).
   - Or open browser → http://127.0.0.1:8000/your-archive.tar.gz
   - Use Copy → Local → Open Document → navigate to Android/data/tech.ula/files/storage → Grant access → Paste

Notes
- If SFTP fails, use HTTP download and SAF paste from Downloads.
- If you have host root (rare), direct cp is possible but dangerous. Prefer SAF.
```