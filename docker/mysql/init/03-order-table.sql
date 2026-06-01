CREATE TABLE IF NOT EXISTS `order_table` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product VARCHAR(200) NOT NULL,
    amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO order_table (user_id, product, amount) VALUES
(1, '笔记本电脑', 6999.00),
(1, '鼠标', 199.00),
(2, '机械键盘', 899.00);
