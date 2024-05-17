@echo off
setlocal enabledelayedexpansion

set "work-dir=C:\Users\26132\Documents\work\winter-framework"
set "src=%work-dir%\src"
set "bin=%work-dir%\bin"
set "lib=%work-dir%\lib"



echo Demarage Jar
jar cf "%work-dir%\winter-framework\winter-frame.jar" -C "%bin%" .
echo Fin Jar