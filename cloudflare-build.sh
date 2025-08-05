#!/bin/bash

# Exit on error
set -e

# Install Clojure CLI
curl -L -O https://github.com/clojure/brew-install/releases/latest/download/linux-install.sh
chmod +x linux-install.sh
sudo ./linux-install.sh

# Run shadow-cljs release
npx shadow-cljs release app
