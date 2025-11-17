#!/bin/bash

echo "ALTER TABLE Sessions DROP FOREIGN KEY FK_Sessions_crx2fasession_id" | mysql CRX
echo "ALTER TABLE Sessions DROP crx2fasession_id;" | mysql CRX
echo "ALTER TABLE Sessions MODIFY validUntil timestamp;" | mysql CRX
echo "ALTER TABLE Sessions MODIFY validFrom timestamp;" | mysql CRX
echo "ALTER TABLE Crx2faSessions DROP FOREIGN KEY  FK_Sessions_session_id;" | mysql CRX
echo "ALTER TABLE Crx2faSessions DROP session_id;" | mysql CRX
echo "ALTER TABLE Users RENAME COLUMN telefonNumber TO telephoneNumber;" | mysql CRX

