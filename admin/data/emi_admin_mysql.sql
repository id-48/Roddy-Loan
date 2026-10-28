-- phpMyAdmin import file
-- Database: u387533797_blastmohanloan
--
-- IMPORTANT:
-- 1) phpMyAdmin me pehle left side se database select karo: u387533797_blastmohanloan
-- 2) Phir Import tab -> Choose File -> yeh .sql file select karo
-- 3) Go / Import
--
-- Do NOT upload emi_admin.sqlite — woh MySQL nahi hai.
-- CREATE DATABASE yahan nahi hai (Hostinger pe DB pehle se bani hoti hai).

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `pending_notifications`;
DROP TABLE IF EXISTS `fcm_devices`;
DROP TABLE IF EXISTS `notification_logs`;
DROP TABLE IF EXISTS `app_open_events`;
DROP TABLE IF EXISTS `app_open_devices`;

CREATE TABLE `app_open_devices` (
  `device_id` VARCHAR(64) NOT NULL,
  `audience` VARCHAR(32) NOT NULL DEFAULT 'organic',
  `open_count` INT NOT NULL DEFAULT 0,
  `first_opened_at` DATETIME NOT NULL,
  `last_opened_at` DATETIME NOT NULL,
  PRIMARY KEY (`device_id`),
  KEY `idx_audience` (`audience`),
  KEY `idx_last_opened` (`last_opened_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `app_open_events` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `device_id` VARCHAR(64) NOT NULL,
  `audience` VARCHAR(32) NOT NULL,
  `opened_at` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_device` (`device_id`),
  KEY `idx_opened` (`opened_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `notification_logs` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(190) NOT NULL,
  `body` TEXT NOT NULL,
  `image_url` TEXT NULL,
  `main_link` TEXT NULL,
  `devices_at_send` INT NOT NULL DEFAULT 0,
  `sent_at` DATETIME NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `fcm_devices` (
  `device_id` VARCHAR(64) NOT NULL,
  `fcm_token` TEXT NULL,
  `updated_at` DATETIME NOT NULL,
  PRIMARY KEY (`device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `pending_notifications` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(190) NOT NULL,
  `body` TEXT NOT NULL,
  `image_url` TEXT NULL,
  `main_link` TEXT NULL,
  `created_at` DATETIME NOT NULL,
  `active` TINYINT(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  KEY `idx_active` (`active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;
