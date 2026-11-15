USE scentstation;
-- MySQL dump 10.13  Distrib 8.0.42, for Win64 (x86_64)
--
-- Host: localhost    Database: scentstation
-- ------------------------------------------------------
-- Server version	8.0.42

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Dumping data for table `alerts`
--

LOCK TABLES `alerts` WRITE;
/*!40000 ALTER TABLE `alerts` DISABLE KEYS */;
INSERT INTO `alerts` VALUES (1,1,2,NULL,1,NULL,'MACHINE_CONNECTION','ERROR','Máy SCENT-M002 mất kết nối','Máy không gửi tín hiệu về máy chủ trong thời gian quy định.','OPEN','2026-11-16 02:31:24',NULL,NULL,'2026-11-16 03:01:24','2026-11-16 03:01:24'),(2,1,4,10,NULL,NULL,'LOW_STOCK','WARNING','Dung tích nước hoa sắp hết','Slot 3 của máy SCENT-M01 có lượng nước hoa thấp hơn ngưỡng cảnh báo.','ACKNOWLEDGED','2026-11-16 03:01:24',NULL,NULL,'2026-11-16 03:01:24','2026-11-16 03:01:24'),(3,2,7,NULL,2,NULL,'HARDWARE_ERROR','CRITICAL','Lỗi phần cứng máy AROMA-M02','Máy phát hiện lỗi phần cứng, cần kỹ thuật viên kiểm tra.','OPEN','2026-11-16 03:01:24',NULL,NULL,'2026-11-16 03:01:24','2026-11-16 03:01:24');
/*!40000 ALTER TABLE `alerts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `audit_logs`
--

LOCK TABLES `audit_logs` WRITE;
/*!40000 ALTER TABLE `audit_logs` DISABLE KEYS */;
INSERT INTO `audit_logs` VALUES (1,2,NULL,'CREATE','MAINTENANCE_TICKET',2,NULL,'{\"title\": \"Kiểm tra lỗi phần cứng AROMA-M02\", \"status\": \"IN_PROGRESS\", \"priority\": \"CRITICAL\"}','127.0.0.1','ScentStation Demo Seed','2026-11-16 03:01:24');
/*!40000 ALTER TABLE `audit_logs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `device_events`
--

LOCK TABLES `device_events` WRITE;
/*!40000 ALTER TABLE `device_events` DISABLE KEYS */;
INSERT INTO `device_events` VALUES (1,2,'CONNECTION_LOST','ERROR','DEMO-CONN-001','Machine lost connection to ScentStation server','{\"signal\": 0, \"source\": \"DEMO\"}','2026-11-16 02:31:24','2026-11-16 02:32:24'),(2,7,'DEVICE_ERROR','CRITICAL','DEMO-ERROR-001','Machine reported an internal hardware error','{\"source\": \"DEMO\", \"errorCode\": \"HW-001\"}','2026-11-16 03:01:24','2026-11-16 03:01:24');
/*!40000 ALTER TABLE `device_events` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `dispense_commands`
--

LOCK TABLES `dispense_commands` WRITE;
/*!40000 ALTER TABLE `dispense_commands` DISABLE KEYS */;
/*!40000 ALTER TABLE `dispense_commands` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `dispense_executions`
--

LOCK TABLES `dispense_executions` WRITE;
/*!40000 ALTER TABLE `dispense_executions` DISABLE KEYS */;
/*!40000 ALTER TABLE `dispense_executions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `inventory_transactions`
--

LOCK TABLES `inventory_transactions` WRITE;
/*!40000 ALTER TABLE `inventory_transactions` DISABLE KEYS */;
INSERT INTO `inventory_transactions` VALUES (1,3,NULL,NULL,1,NULL,'REFILL',25.00,'SEED-DEMO-INVENTORY-REFILL','2026-11-16 03:03:12');
/*!40000 ALTER TABLE `inventory_transactions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `locations`
--

LOCK TABLES `locations` WRITE;
/*!40000 ALTER TABLE `locations` DISABLE KEYS */;
INSERT INTO `locations` VALUES (1,1,'Scent Demo - District 1','123 Nguyen Hue, District 1, Ho Chi Minh City','Demo location for ScentStation','ACTIVE','2026-11-15 01:26:11','2026-11-15 01:26:11'),(2,1,'Scent Demo - District 3','45 Vo Van Tan, District 3, Ho Chi Minh City','Second demo location','ACTIVE','2026-11-15 01:26:11','2026-11-15 01:26:11'),(3,2,'Aroma House - Demo Mall','88 Le Loi, District 1, Ho Chi Minh City','Aroma House demo location','ACTIVE','2026-11-15 01:26:11','2026-11-15 01:26:11'),(4,1,'SCENT Landmark 81','Vinhomes Central Park, Binh Thanh, Ho Chi Minh City','Diem trai nghiem nuoc hoa tai Landmark 81','ACTIVE','2026-11-16 02:44:48','2026-11-16 02:44:48'),(5,1,'SCENT University','Thu Duc City, Ho Chi Minh City','Diem trai nghiem nuoc hoa trong khuon vien dai hoc','ACTIVE','2026-11-16 02:44:48','2026-11-16 02:44:48'),(6,2,'AROMA Shopping Mall','District 1, Ho Chi Minh City','Diem trai nghiem nuoc hoa tai trung tam thuong mai','ACTIVE','2026-11-16 02:44:48','2026-11-16 02:44:48'),(7,2,'AROMA Office','Thu Duc City, Ho Chi Minh City','Diem trai nghiem nuoc hoa tai toa nha van phong','ACTIVE','2026-11-16 02:44:48','2026-11-16 02:44:48');
/*!40000 ALTER TABLE `locations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `machine_slots`
--

LOCK TABLES `machine_slots` WRITE;
/*!40000 ALTER TABLE `machine_slots` DISABLE KEYS */;
INSERT INTO `machine_slots` VALUES (1,1,1,1,100.00,80.00,15.00,'AVAILABLE','2026-11-15 01:26:11','2026-11-15 01:26:11'),(2,1,2,2,100.00,65.00,15.00,'AVAILABLE','2026-11-15 01:26:11','2026-11-15 01:26:11'),(3,1,3,3,100.00,37.00,15.00,'AVAILABLE','2026-11-15 01:26:11','2026-11-16 03:03:12'),(4,2,1,1,100.00,90.00,15.00,'AVAILABLE','2026-11-15 01:26:11','2026-11-15 01:26:11'),(5,2,2,3,100.00,55.00,15.00,'AVAILABLE','2026-11-15 01:26:11','2026-11-15 01:26:11'),(6,3,1,4,100.00,75.00,15.00,'AVAILABLE','2026-11-15 01:26:11','2026-11-15 01:26:11'),(7,3,2,5,100.00,60.00,15.00,'AVAILABLE','2026-11-15 01:26:11','2026-11-15 01:26:11'),(8,4,1,1,100.00,80.00,15.00,'AVAILABLE','2026-11-16 02:45:58','2026-11-16 02:45:58'),(9,4,2,2,100.00,8.00,15.00,'LOW_STOCK','2026-11-16 02:45:58','2026-11-16 02:45:58'),(10,4,3,3,100.00,0.00,15.00,'EMPTY','2026-11-16 02:45:58','2026-11-16 02:45:58'),(11,5,1,1,100.00,70.00,15.00,'AVAILABLE','2026-11-16 02:45:58','2026-11-16 02:45:58'),(12,5,2,2,100.00,50.00,15.00,'AVAILABLE','2026-11-16 02:45:58','2026-11-16 02:45:58'),(13,5,3,3,100.00,20.00,15.00,'DISABLED','2026-11-16 02:45:58','2026-11-16 02:45:58'),(14,6,1,4,100.00,90.00,15.00,'AVAILABLE','2026-11-16 02:45:58','2026-11-16 02:45:58'),(15,6,2,5,100.00,10.00,15.00,'LOW_STOCK','2026-11-16 02:45:58','2026-11-16 02:45:58'),(16,6,3,8,100.00,0.00,15.00,'EMPTY','2026-11-16 02:45:58','2026-11-16 02:45:58'),(17,7,1,4,100.00,60.00,15.00,'AVAILABLE','2026-11-16 02:45:58','2026-11-16 02:45:58'),(18,7,2,5,100.00,40.00,15.00,'AVAILABLE','2026-11-16 02:45:58','2026-11-16 02:45:58'),(19,7,3,8,100.00,30.00,15.00,'ERROR','2026-11-16 02:45:58','2026-11-16 02:45:58');
/*!40000 ALTER TABLE `machine_slots` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `machines`
--

LOCK TABLES `machines` WRITE;
/*!40000 ALTER TABLE `machines` DISABLE KEYS */;
INSERT INTO `machines` VALUES (1,1,1,'SCENT-M001','SCENT-SN-0001','scent-device-0001','ONLINE','ACTIVE','1.0.0','2026-11-15 01:26:11','2026-11-15 01:26:11','2026-11-15 01:26:11'),(2,1,2,'SCENT-M002','SCENT-SN-0002','scent-device-0002','ONLINE','ACTIVE','1.0.0','2026-11-15 01:26:11','2026-11-15 01:26:11','2026-11-15 01:26:11'),(3,2,3,'AROMA-M001','AROMA-SN-0001','aroma-device-0001','ONLINE','ACTIVE','1.0.0','2026-11-15 01:26:11','2026-11-15 01:26:11','2026-11-15 01:26:11'),(4,1,4,'SCENT-M01','SN-SCENT-001','DEVICE-SCENT-001','ONLINE','ACTIVE','1.0.0','2026-11-16 02:45:47','2026-11-16 02:45:47','2026-11-16 02:45:47'),(5,1,5,'SCENT-M02','SN-SCENT-002','DEVICE-SCENT-002','OFFLINE','MAINTENANCE','1.0.0',NULL,'2026-11-16 02:45:47','2026-11-16 02:45:47'),(6,2,6,'AROMA-M01','SN-AROMA-001','DEVICE-AROMA-001','ONLINE','ACTIVE','1.1.0','2026-11-16 02:45:47','2026-11-16 02:45:47','2026-11-16 02:45:47'),(7,2,7,'AROMA-M02','SN-AROMA-002','DEVICE-AROMA-002','OFFLINE','ERROR','1.1.0',NULL,'2026-11-16 02:45:47','2026-11-16 02:45:47');
/*!40000 ALTER TABLE `machines` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `maintenance_logs`
--

LOCK TABLES `maintenance_logs` WRITE;
/*!40000 ALTER TABLE `maintenance_logs` DISABLE KEYS */;
INSERT INTO `maintenance_logs` VALUES (1,2,NULL,'INSPECTION_STARTED','Đã tiếp nhận yêu cầu và bắt đầu kiểm tra thiết bị.','2026-11-16 03:01:24');
/*!40000 ALTER TABLE `maintenance_logs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `maintenance_tickets`
--

LOCK TABLES `maintenance_tickets` WRITE;
/*!40000 ALTER TABLE `maintenance_tickets` DISABLE KEYS */;
INSERT INTO `maintenance_tickets` VALUES (1,1,2,1,NULL,NULL,'Kiểm tra kết nối máy SCENT-M002','Kiểm tra nguồn điện, mạng và trạng thái kết nối của máy.','HIGH','OPEN','2026-11-16 02:36:24',NULL,NULL,'2026-11-16 03:01:24','2026-11-16 03:01:24'),(2,2,7,3,NULL,NULL,'Kiểm tra lỗi phần cứng AROMA-M02','Kiểm tra bo mạch điều khiển và cảm biến thiết bị.','CRITICAL','IN_PROGRESS','2026-11-16 01:01:24','2026-11-16 02:01:24',NULL,'2026-11-16 03:01:24','2026-11-16 03:01:24');
/*!40000 ALTER TABLE `maintenance_tickets` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `notifications`
--

LOCK TABLES `notifications` WRITE;
/*!40000 ALTER TABLE `notifications` DISABLE KEYS */;
INSERT INTO `notifications` VALUES (1,1,NULL,'MACHINE_ALERT','Máy SCENT-M002 mất kết nối','Vui lòng kiểm tra trạng thái kết nối của máy SCENT-M002.','MACHINE',2,0,NULL,'2026-11-16 03:01:24'),(2,2,NULL,'MAINTENANCE','Phiếu bảo trì mới','Có phiếu bảo trì cần được xử lý cho máy AROMA-M02.','MACHINE',7,0,NULL,'2026-11-16 03:01:24');
/*!40000 ALTER TABLE `notifications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
INSERT INTO `orders` VALUES (1,1,1,1,1,'Citrus Morning',5000.00,'VND','PAID',NULL,'2026-11-15 01:26:11','2026-11-15 01:26:11'),(2,1,1,2,2,'Ocean Breeze',6000.00,'VND','PENDING_PAYMENT','2026-11-15 01:41:11','2026-11-15 01:26:11','2026-11-15 01:26:11');
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `payments`
--

LOCK TABLES `payments` WRITE;
/*!40000 ALTER TABLE `payments` DISABLE KEYS */;
INSERT INTO `payments` VALUES (1,1,'PAY-DEMO-SUCCESS-001','DEMO_GATEWAY',5000.00,'VND','SUCCESS','GW-DEMO-SUCCESS-001','{\"demo\": true}','2026-11-15 01:26:11','2026-11-15 01:26:11','2026-11-15 01:26:11'),(2,2,'PAY-DEMO-PENDING-001','DEMO_GATEWAY',6000.00,'VND','PENDING',NULL,'{\"demo\": true}',NULL,'2026-11-15 01:26:11','2026-11-15 01:26:11');
/*!40000 ALTER TABLE `payments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `products`
--

LOCK TABLES `products` WRITE;
/*!40000 ALTER TABLE `products` DISABLE KEYS */;
INSERT INTO `products` VALUES (1,1,'SCENT-001','Citrus Morning','Fresh citrus fragrance','Bergamot, lemon, green tea',NULL,5000.00,0.12,'ACTIVE','2026-11-15 01:26:11','2026-11-15 01:26:11'),(2,1,'SCENT-002','Ocean Breeze','Clean aquatic fragrance','Sea salt, marine accord, cedarwood',NULL,6000.00,0.12,'ACTIVE','2026-11-15 01:26:11','2026-11-15 01:26:11'),(3,1,'SCENT-003','Velvet Rose','Soft floral fragrance','Rose, peony, musk',NULL,7000.00,0.12,'ACTIVE','2026-11-15 01:26:11','2026-11-15 01:26:11'),(4,2,'AROMA-001','Amber Woods','Warm woody fragrance','Amber, sandalwood, vanilla',NULL,8000.00,0.15,'ACTIVE','2026-11-15 01:26:11','2026-11-15 01:26:11'),(5,2,'AROMA-002','White Jasmine','Light floral fragrance','Jasmine, white tea, musk',NULL,7500.00,0.15,'ACTIVE','2026-11-15 01:26:11','2026-11-15 01:26:11'),(8,2,'AROMA-003','Midnight Rose','Huong hoa hong quy phai','Rose, Patchouli, Musk',NULL,22000.00,0.20,'ACTIVE','2026-11-16 02:45:14','2026-11-16 02:45:14');
/*!40000 ALTER TABLE `products` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `refills`
--

LOCK TABLES `refills` WRITE;
/*!40000 ALTER TABLE `refills` DISABLE KEYS */;
INSERT INTO `refills` VALUES (1,3,3,NULL,25.00,'SEED-DEMO-REFILL-SCENT-M001-SLOT-3','2026-11-16 03:03:12','2026-11-16 03:03:12');
/*!40000 ALTER TABLE `refills` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `roles`
--

LOCK TABLES `roles` WRITE;
/*!40000 ALTER TABLE `roles` DISABLE KEYS */;
INSERT INTO `roles` VALUES (1,'PLATFORM_SUPER_ADMIN','Platform Super Admin','Quan tri toan bo nen tang','2026-10-28 17:33:51'),(2,'BRAND_ADMIN','Brand Admin','Quan tri thuong hieu','2026-10-28 17:33:51'),(3,'OPERATIONS_MANAGER','Operations Manager','Quan ly hoat dong may','2026-10-28 17:33:51'),(4,'TECHNICIAN','Technician','Bao tri thiet bi IoT','2026-10-28 17:33:51'),(5,'INVENTORY_STAFF','Inventory Staff','Quan ly ton kho','2026-10-28 17:33:51');
/*!40000 ALTER TABLE `roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `sensor_readings`
--

LOCK TABLES `sensor_readings` WRITE;
/*!40000 ALTER TABLE `sensor_readings` DISABLE KEYS */;
INSERT INTO `sensor_readings` VALUES (1,1,1,'LIQUID_LEVEL',80.0000,'ml','2026-11-16 02:00:00'),(2,1,1,'SPRAY_COUNT',125.0000,'count','2026-11-16 02:00:00'),(3,1,1,'TEMPERATURE',25.5000,'C','2026-11-16 02:00:00'),(4,1,2,'LIQUID_LEVEL',65.0000,'ml','2026-11-16 02:00:00'),(5,1,2,'SPRAY_COUNT',98.0000,'count','2026-11-16 02:00:00'),(6,1,2,'TEMPERATURE',26.0000,'C','2026-11-16 02:00:00'),(7,1,3,'LIQUID_LEVEL',37.0000,'ml','2026-11-16 02:00:00'),(8,1,3,'SPRAY_COUNT',45.0000,'count','2026-11-16 02:00:00'),(9,1,3,'TEMPERATURE',25.0000,'C','2026-11-16 02:00:00');
/*!40000 ALTER TABLE `sensor_readings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `tenants`
--

LOCK TABLES `tenants` WRITE;
/*!40000 ALTER TABLE `tenants` DISABLE KEYS */;
INSERT INTO `tenants` VALUES (1,'ScentStation Demo Updated','SCENT-DEMO','ACTIVE','2026-10-28 17:33:51','2026-10-28 17:33:51'),(2,'Aroma House Demo','AROMA-DEMO','ACTIVE','2026-11-15 01:26:11','2026-11-15 01:26:11');
/*!40000 ALTER TABLE `tenants` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `user_roles`
--

LOCK TABLES `user_roles` WRITE;
/*!40000 ALTER TABLE `user_roles` DISABLE KEYS */;
INSERT INTO `user_roles` VALUES (1,2,'2026-11-12 19:39:53'),(2,1,'2026-11-15 01:26:11'),(3,3,'2026-11-15 01:26:11'),(4,4,'2026-11-15 01:26:11'),(5,5,'2026-11-15 01:26:11'),(6,2,'2026-11-15 01:26:11');
/*!40000 ALTER TABLE `user_roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,1,'brandadmin','admin@scentstation.com','$2a$10$4sXQgDcStkJObCJY3mVXbu.ZCTu3tKFlpNpczNwioiqr3UBYdb7Au','Brand Administrator','ACTIVE','2026-11-12 19:39:53','2026-11-12 19:39:53'),(2,NULL,'platformadmin','platform@scentstation.com','$2a$10$4sXQgDcStkJObCJY3mVXbu.ZCTu3tKFlpNpczNwioiqr3UBYdb7Au','Platform Administrator','ACTIVE','2026-11-15 01:26:11','2026-11-15 01:26:11'),(3,1,'ops_scent','ops.updated@scentstation.com','$2a$10$4sXQgDcStkJObCJY3mVXbu.ZCTu3tKFlpNpczNwioiqr3UBYdb7Au','Scent Operations Manager Updated','INACTIVE','2026-11-15 01:26:11','2026-11-16 02:21:38'),(4,1,'tech_scent','tech@scentstation.com','$2a$10$4sXQgDcStkJObCJY3mVXbu.ZCTu3tKFlpNpczNwioiqr3UBYdb7Au','Scent Technician','ACTIVE','2026-11-15 01:26:11','2026-11-15 01:26:11'),(5,1,'stock_scent','stock@scentstation.com','$2a$10$4sXQgDcStkJObCJY3mVXbu.ZCTu3tKFlpNpczNwioiqr3UBYdb7Au','Scent Inventory Staff','ACTIVE','2026-11-15 01:26:11','2026-11-15 01:26:11'),(6,2,'admin_aroma','admin.updated@aromahouse.demo','$2a$10$4sXQgDcStkJObCJY3mVXbu.ZCTu3tKFlpNpczNwioiqr3UBYdb7Au','Aroma House Admin Updated','ACTIVE','2026-11-15 01:26:11','2026-11-16 01:28:20');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-11-16  3:12:22
