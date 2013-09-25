set :application, 'sbpm'
set :repo_url, 'https://bitbucket.org/sbpmgroupware/sbpm.git'

set :branch, "amazon_instanzen"

set :scm, :git
set :deploy_via, :remote_cach

set :ssh_options, { forward_agent: true }

set :use_sudo, false
set :keep_releases, 3

set :default_stage, "staging"

set :artifact_host_url, "ubuntu2@sbpm-gw.tk.informatik.tu-darmstadt.de"

set :artifacts_path, -> { Pathname.new("/home/ubuntu2/apps/sbpm/artifacts/#{fetch(:stage)}") }

set :backend_artifact_path, fetch(:artifacts_path).join('backend.jar')
set :repository_artifact_path, fetch(:artifacts_path).join('repository.jar')

set :linked_dirs, %w[ pids log ]

set :format, :pretty

set :log_level, :debug
set :pty, false
