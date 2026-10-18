import io
import sys

import pycdlib

elf, out = sys.argv[1], sys.argv[2]
iso = pycdlib.PyCdlib()
iso.new(interchange_level=1, sys_ident="PLAYSTATION", vol_ident="WLAUNCHELF")
cnf = "BOOT2 = cdrom0:" + chr(92) + "SLUS_209.99;1\r\nVER = 1.00\r\nVMODE = NTSC\r\n"
cnf = cnf.encode()
iso.add_fp(io.BytesIO(cnf), len(cnf), "/SYSTEM.CNF;1")
data = open(elf, "rb").read()
iso.add_fp(io.BytesIO(data), len(data), "/SLUS_209.99;1")
iso.write(out)
iso.close()
print("wrote", out)
