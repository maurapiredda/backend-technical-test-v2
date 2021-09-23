insert into address (id, street, postcode, city, country) values (1, '785 Cabell Avenue', '23219', 'Raccoon', 'USA');

insert into pilotes_order(id, order_number, address_id, pilotes_number, total, creation_date) values (1, 'n1', 1, 'FIVE', 5.0,  {ts '2021-09-20T15:00:00Z'});
insert into pilotes_order(id, order_number, address_id, pilotes_number, total, creation_date) values (2, 'n2', 1, 'TEN', 10.0,  {ts '2021-09-21T15:10:00Z'});
insert into pilotes_order(id, order_number, address_id, pilotes_number, total, creation_date) values (3, 'n3', 1, 'FIVE', 5.0,  {ts '2021-09-22T15:20:00Z'});
