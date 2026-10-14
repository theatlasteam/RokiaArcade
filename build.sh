#!/bin/sh
set -eu
cd "$(dirname "$0")"
rm -rf build/classes build/preverified
mkdir -p build/classes dist
J2CN=../j2cn
N2D=../nitro2d
NDASH=../nokodash/src/ndash/BitmapFont.java
java -jar ../lib/ecj.jar -source 1.3 -target 1.3 -bootclasspath ../lib/cldcapi11.jar -classpath ../lib/midpapi20.jar -d build/classes $J2CN/src/j2cn/*.java $N2D/src/n2d/*.java $NDASH src/arcade/*.java
java -jar ../lib/proguard.jar -injars build/classes -outjars build/preverified -libraryjars ../lib/cldcapi11.jar -libraryjars ../lib/midpapi20.jar -dontshrink -dontoptimize -dontobfuscate -microedition
python3 - <<'PY'
from pathlib import Path
import zipfile
fields=['MIDlet-Name: RokiaArcade','MIDlet-Version: 0.1.0','MIDlet-Vendor: Atlas','MIDlet-1: RokiaArcade,/icon.png,arcade.Arcade','MicroEdition-Configuration: CLDC-1.1','MicroEdition-Profile: MIDP-2.0']
jar=Path('dist/RokiaArcade.jar')
with zipfile.ZipFile(jar,'w',zipfile.ZIP_DEFLATED) as z:
 z.writestr('META-INF/MANIFEST.MF','\r\n'.join(['Manifest-Version: 1.0']+fields+['','']))
 z.write('assets/icon.png','icon.png');z.write('assets/font16.png','font16.png');z.write('assets/font16.tbl','font16.tbl')
 for f in sorted(Path('build/preverified').rglob('*.class')):z.write(f,f.relative_to('build/preverified').as_posix())
with zipfile.ZipFile(jar) as z:
 assert z.testzip() is None
 for name in z.namelist():
  if name.endswith('.class'):assert int.from_bytes(z.read(name)[6:8],'big')==47
Path('dist/RokiaArcade.jad').write_bytes(('\r\n'.join(fields+['MIDlet-Jar-URL: RokiaArcade.jar','MIDlet-Jar-Size: '+str(jar.stat().st_size),''])).encode('ascii'))
print('Built',jar,jar.stat().st_size,'bytes; class version 47 and ZIP verified')
PY
