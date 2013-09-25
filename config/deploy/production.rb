set :stage, :production

role :app, %w[
   ubuntu@ec2-54-229-92-171.eu-west-1.compute.amazonaws.com
   ubuntu@ec2-54-229-91-177.eu-west-1.compute.amazonaws.com
   ubuntu@ec2-54-229-82-150.eu-west-1.compute.amazonaws.com
]

role :repo_host, %w[ ubuntu2@sbpm-gw.tk.informatik.tu-darmstadt.de ]

set :deploy_to, "/home/ubuntu/apps/sbpm"

set :artifact_branch, "amazon_instanzen"

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
      execute "ln -s #{shared_path}/pids #{release_path}/pids"
      execute "ln -s #{shared_path}/log #{release_path}/log"
    end
  end
end
