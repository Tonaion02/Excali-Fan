wget https://downloads.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz
mkdir -p $HOME/maven & tar -xvzf apache-maven-3.9.6-bin.tar.gz -C $HOME/maven
export PATH=$HOME/maven/apache-maven-3.9.6/bin:$PATH 
read -p "Press enter to continue"