i=1;
for user in "$@"
do
    echo "Username - $i: $user";
    i=$((i + 1));
    sleep 1
done
sleep 3
echo "all user listed."