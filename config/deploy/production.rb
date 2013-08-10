set :stage, :production

role :app, %w[ ubuntu@54.229.92.171 ubuntu@54.229.91.177 ubuntu@54.229.82.150 ]
role :repo_host, %w[ ubuntu@54.229.92.171 ]

set :deploy_to, "/home/ubuntu/apps/sbpm"

namespace :artifacts do
  desc "downloads the current jar from the artifact host on every server"
  task :download do
    on roles(:app) do
      execute(:scp,
              "#{fetch(:artifact_host_url)}:#{fetch(:backend_artifact_path)}",
              release_path.join('backend.jar'))
    end
    on roles(:repo_host) do
      execute(:scp,
              "#{fetch(:artifact_host_url)}:#{fetch(:repository_artifact_path)}",
              release_path.join('repository.jar'))
    end
  end
end
