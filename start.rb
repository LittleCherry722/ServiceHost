#!/usr/bin/ruby

require 'fileutils'

Dir.chdir("Backend") do
  system("bin/sbt.sh assembly")
  FileUtils.mv 'target/scala-2.10/sbpm.jar', '../backend.jar'
end
FileUtils.cp 'backend.jar', 'backend_8081.jar'
FileUtils.cp 'backend.jar', 'backend_8082.jar'
FileUtils.mv 'backend.jar', 'backend_8080.jar'

Dir.chdir("Repository") do
  system("bin/sbt.sh assembly")
  FileUtils.mv 'target/scala-2.10/sbpm.jar', '../repository.jar'
end

# system("SBPM_PORT=8080 AKKA_PORT=2552 backend_staging.sh")
# system("SBPM_PORT=8081 AKKA_PORT=2553 backend_staging.sh")
# system("SBPM_PORT=8082 AKKA_PORT=2554 backend_staging.sh")
# system("repository.sh")
