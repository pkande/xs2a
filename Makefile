# https://www.gnu.org/software/make/manual/make.html#Phony-Targets
.PHONY : clean usage pages

usage :
	echo "\"make clean\" or \"make pages\""

pages :
	mkdir -p pages

clean :
	-rm -r pages
