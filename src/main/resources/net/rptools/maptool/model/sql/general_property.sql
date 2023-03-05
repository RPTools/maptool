create table property_collection (
    property_type   varchar(255)  not null,
    namespace       varchar(1024) not null,
    collection_id   identity      not null,
    primary key (property_type, namespace)
);

create table property_definition (
    collection_id   bigint        not null,
    data_id         identity      not null,
    name            varchar(1024) not null,
    type            varchar(20)   not null,
    defined         boolean       not null,
    is_clob         boolean       not null,
    primary key (collection_id, name)
);

create table property_data (
    property_id     identity      not null,
    data_id         bigint        not null,
    numeric_value   double,
    string_value    varchar(1024),
    primary key (property_id, data_id)
);

create table property_data_clob (
    property_id     identity      not null primary key ,
    data_id         bigint        not null,
    clob_value      clob,
    primary key (property_id, data_id)
);

