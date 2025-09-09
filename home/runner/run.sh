# Create a folder
$ mkdir actions-runner && cd actions-runner# Download the latest runner package
$ curl -o actions-runner-linux-arm64-2.328.0.tar.gz -L https://github.com/actions/runner/releases/download/v2.328.0/actions-runner-linux-arm64-2.328.0.tar.gz# Optional: Validate the hash
$ echo "b801b9809c4d9301932bccadf57ca13533073b2aa9fa9b8e625a8db905b5d8eb  actions-runner-linux-arm64-2.328.0.tar.gz" | shasum -a 256 -c# Extract the installer
$ tar xzf ./actions-runner-linux-arm64-2.328.0.tar.gz
Configure
# Create the runner and start the configuration experience
$ ./config.sh --url https://github.com/spiralgang/DevUl-Army--__--Living-Sriracha-AGI --token BOBRGNRCEMIQRTUZJTF4BGDIYAIH6# Last step, run it!
$ ./run.sh
Using your self-hosted runner
# Use this YAML in your workflow file for each job
runs-on: self-hosted
