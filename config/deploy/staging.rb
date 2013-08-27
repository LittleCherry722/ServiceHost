set :stage, :staging

role :app, %w[ ubuntu2@sbpm-gw.tk.informatik.tu-darmstadt.de ]
role :repo_host, %w[ ubuntu2@sbpm-gw.tk.informatik.tu-darmstadt.de ]

set :deploy_to, "/home/ubuntu2/apps/sbpm"

set :artifact_branch, "staging"

set :hostname, "127.0.0.1"

namespace :deploy do
  desc "starts the server"
  task :start do
    on roles(:app), in: :parallel do |host|
      within current_path do
        [
         { sbpm_port: 8081, akka_port: 2553 },
         { sbpm_port: 8082, akka_port: 2554 }
        ].each do |port| 
          execute "cp", "backend.jar", "backend_#{port[:sbpm_port]}"
          with port do
            execute current_path.join('backend.sh')
          end
        end
      end
    end
  end
end

namespace :artifacts do
  desc "downloads the current jar from the artifact host on every server"
  task :download do
    on roles(:app) do
      execute(:cp,
              fetch(:backend_artifact_path),
              release_path.join('backend.jar'))
    end
    on roles(:repo_host) do
      execute(:cp,
              fetch(:repository_artifact_path),
              release_path.join('repository.jar'))
    end
  end
end
