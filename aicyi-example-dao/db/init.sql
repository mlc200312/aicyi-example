create table message_template
(
    id            bigint auto_increment comment '主键'
        primary key,
    template_code varchar(64)                  not null comment '模版编码',
    template_name varchar(128)                 null comment '模版名称',
    message_type  varchar(16)                  not null comment '消息类型',
    format        varchar(16)                  null comment '模版格式',
    engine_type   varchar(32) default 'SIMPLE' not null comment '模版引擎类型',
    subject       varchar(255)                 null comment '模版主题',
    content       text                         not null comment '模版内容',
    signature     varchar(64)                  null comment '短信签名',
    variables     varchar(512)                 null comment '模版参数',
    enabled       tinyint     default 1        null comment '是否启用，0:未启用，1:已启用；',
    remark        varchar(255)                 null comment '备注',
    deleted       tinyint     default 0        not null comment '删除标记，0：未删除，1：已删除',
    version       int         default 0        not null comment '版本',
    create_time   datetime                     not null comment '创建时间',
    update_time   datetime                     not null comment '更新时间',
    constraint uk_template_code
        unique (template_code)
)
    comment '消息模版表';

create index idx_message_type
    on message_template (message_type);