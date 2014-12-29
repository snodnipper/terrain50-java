#! /usr/bin/python

################################################################################
# Rename all files in ./data to something consistent.  Arbitrary dates cannot
# be calculated by clients and thus must be removed.
################################################################################

import os, os.path, re

counterAll = 0
counterRenamed = 0

for root, _, files in os.walk("./data"):
    for f in files:
        counterAll += 1
        fullpath = os.path.join(root, f)
        replaced = re.sub("_OST50GRID\w*", "", f)
        newfullpath = os.path.join(root, replaced)

        if fullpath == newfullpath:
            print 'Warning: not renaming ' + fullpath
        else:
            # print 'Renamed: "' + fullpath + '" to "' + newfullpath
            counterRenamed += 1
        os.rename(fullpath, newfullpath)


if counterAll == counterRenamed:
    print 'Done. Renamed all ' + str(counterAll) + 'files'
else:
    print 'Done. Renamed ' + str(counterRenamed) + ' of ' + str(counterAll) + ' files.'
