#!/bin/bash

export HOME="/root/"

echo "ALTER TABLE  CrxNextID ADD COLUMN IF NOT EXISTS recTime DATETIME NOT NULL DEFAULT NOW();" | mysql CRX

