#
# Under t/csh use this script by calling 
#   source classpath.csh
#
# written by Aleksander Slominski [http://www.extreme.indiana.edu/~aslom]
#

setenv CLASSPATH `$PWD/classpath.sh $*`
echo $CLASSPATH
