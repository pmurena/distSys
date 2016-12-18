
import glob

files = glob.glob("./*.txt")

newFile = ""

for file in files:
    with open(file, 'r') as inputFile:
        for line in inputFile:
            newFile += line
        newFile += "\n\n"

with open('allFiles.txt', 'w') as outPutFile:
    outPutFile.write(newFile)
