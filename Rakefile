# Ruby Rake file to allow automatic build testing on RunCodeRun.com
 
task :default => [:test]
 
 
#-classpath "${M2_HOME}"/boot/classworlds-*.jar \
#  "-Dclassworlds.conf=${M2_HOME}/bin/m2.conf" \
#  "-Dmaven.home=${M2_HOME}"  \
#  ${CLASSWORLDS_LAUNCHER} $QUOTED_ARGS
 
task :test do
  m2_home = File.join(".", "maven")
  classpath = File.join(".", "maven", "boot", "classworlds-1.1.jar")
  conf = File.join(".", "maven", "bin", "m2.conf")
  system "java -classpath #{classpath} -Dclassworlds.conf=#{conf} -Dmaven.home=#{m2_home} org.codehaus.classworlds.Launcher clean install"
end
