Quy trình update sau này (main + vnso)

cd /root/cloudstack
git checkout main
git fetch upstream
git merge --ff-only upstream/main
git push origin main

git checkout vnso
git rebase main   # hoặc: git merge main
git push origin vnso