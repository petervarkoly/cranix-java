#!/bin/bash

echo "ALTER TABLE Sessions DROP FOREIGN KEY FK_Sessions_crx2fasession_id" | mysql CRX
echo "ALTER TABLE Sessions DROP crx2fasession_id;" | mysql CRX

