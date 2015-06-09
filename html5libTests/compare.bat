@echo off REM do not display everything to the window

REM variables
set R=C:\Users\hs012\Desktop\html5libTests
set E=expected
set J=jsoup
set C=expected_vs_jsoup
set Z=C:\gnuwin32\bin
set O=output.txt

cd %R%
REM create folder for comparison - expected_vs_jsoup
mkdir %C%
cd %E%
@echo off

REM loop through directories - use /d
for /d %%d in (*) do (
	mkdir %R%\%C%\%%d
	@echo %%d >> %R%\%C%\%O%
	@echo off
	REM loop through files 
	for %%f in (%%d\*.txt) do (
		REM check for differences and save to a file
		%Z%\diff %%f %R%\%J%\%%f > %R%\%C%\%%f
	)
	REM delete empty files (no differences)
	REM ~n file name, ~x file extension (can be combined e.g. %%~nxf)
	for %%f in (%R%\%C%\%%d\*) do (
		if not %%~zf EQU 0 (
			@echo %%~nf  X >> %R%\%C%\%O%
		)
		if %%~zf EQU 0 (
			@echo %%~nf  O >> %R%\%C%\%O%
			del %%f
		)
		@echo off
	)
	@echo --------------- >> %R%\%C%\%O%
	@echo saved %%d
)

REM wait for a key press to exit
pause