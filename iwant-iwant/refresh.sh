# TODO maybe iwant will handle all this "refresh stuff"...
# (it will, the whole 1st generation bootstrapper won't live long anymore)
rm -rf iwant/as-iwant-user
rm -rf iwant/as-iwant-developer
rm -rf iwant/cached/iwant/cpitems
rm -rf iwant/cached/iwant/local-ant-bootstrapping-tutorial
rm -rf iwant/cached/iwant/local-bash-bootstrapping-tutorial
rm -rf iwant/cached/iwant/local-tutorial
rm -rf iwant/cached/iwant/scripts
rm -rf iwant/cached/iwant/testarea
iwant/as_shell-user/to-bootstrap-iwant.sh && iwant/as-iwant-user/to-develop-iwant.sh && iwant/as-iwant-developer/target/local-website/as-path
