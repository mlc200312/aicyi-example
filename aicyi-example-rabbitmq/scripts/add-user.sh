docker exec rabbitmq sh -c "
  rabbitmqctl add_user test test
  rabbitmqctl set_permissions -p / test '.*' '.*' '.*'   # /  vhost 的读写+配置权限
  rabbitmqctl set_user_tags test None"