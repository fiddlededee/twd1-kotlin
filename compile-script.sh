cp $(kscript -p build.main.kts 2> >(grep -oP -i '(?<=\[kscript\] )\/.*')).jar \
  ./build.main.jar
