"""Prepare public source metadata only after the bundle and tests have succeeded."""
import datetime
import hashlib
import json
import os
import pathlib
import re

root = pathlib.Path(__file__).resolve().parents[2]
properties = (root / "gradle.properties").read_text(encoding="utf-8")
version = re.search(r"^version\s*=\s*([0-9]+\.[0-9]+\.[0-9]+)$", properties, re.M).group(1)
repository = os.environ["GITHUB_REPOSITORY"]
bundles = [path for path in (root / "patches/build/libs").glob("*.mpp")
           if not path.name.endswith(("-sources.mpp", "-javadoc.mpp"))]
assert len(bundles) == 1, f"Expected exactly one bundle, found {bundles}"
bundle = bundles[0]
assert "Fold landscape 4:3 layout" in (root / "patches-list.json").read_text(encoding="utf-8")
metadata = {
    "created_at": datetime.datetime.now(datetime.timezone.utc).strftime("%Y-%m-%dT%H:%M:%S"),
    "description": "Piko Fold : post à gauche, réponses à droite, rail stable et messages classiques à deux panneaux en paysage 4:3. "
                   "X 12.19.1-release.0. Sélectionner Fold landscape 4:3 layout. "
                   "Fork de crimera/piko ; XChat avec mouvements réduits. Juxtaposition des messages selon le support système.",
    "download_url": f"https://github.com/{repository}/releases/download/v{version}/{bundle.name}",
    "version": version,
}
(root / "patches-bundle.json").write_text(json.dumps(metadata, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
digest = hashlib.sha256(bundle.read_bytes()).hexdigest()
(root / "SHA256SUMS").write_text(f"{digest}  {bundle.name}\n", encoding="utf-8")
print(f"Prepared Morphe source {repository} v{version}: {bundle.name}")
