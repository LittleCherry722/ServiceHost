#!/usr/bin/ruby

require 'fileutils'

FileUtils.mkdir "pids" rescue puts "Pids dir alreadys exists"
FileUtils.mkdir "log" rescue puts "Logs dir alreadys exists"

Dir.chdir "pids" do
  begin
    system "find *.pid -print | xargs cat | xargs kill"
  rescue
    puts "Nothing to restart"
  ensure
    system "rm *.pid"
  end
end

Dir.chdir("Backend") do
  system("bin/sbt.sh assembly")
  FileUtils.mv 'target/scala-2.10/sbpm.jar', 'backend.jar'

  FileUtils.cp 'backend.jar', 'backend_8081.jar'
  FileUtils.cp 'backend.jar', 'backend_8082.jar'
  FileUtils.mv 'backend.jar', 'backend_8080.jar'
end

Dir.chdir("Repository") do
  system("bin/sbt.sh assembly")
  FileUtils.mv 'target/scala-2.10/sbpm.jar', '../repository.jar'
end

system("SBPM_PORT=8080 AKKA_PORT=2552 sh backend_staging.sh")
system("SBPM_PORT=8081 AKKA_PORT=2553 sh backend_staging.sh")
system("SBPM_PORT=8082 AKKA_PORT=2554 sh backend_staging.sh")
system("sh repository.sh")
