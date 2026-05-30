IF DB_ID('StoreManagement1') IS NULL
BEGIN
    CREATE DATABASE StoreManagement1;
END
GO

USE StoreManagement1;
GO

IF NOT EXISTS (SELECT * FROM sys.schemas WHERE name = 'management')
BEGIN
    EXEC('CREATE SCHEMA management');
END
GO

IF NOT EXISTS (SELECT * FROM sys.schemas WHERE name = 'customers')
BEGIN
    EXEC('CREATE SCHEMA customers');
END
GO
