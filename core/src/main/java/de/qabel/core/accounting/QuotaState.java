package de.qabel.core.accounting;

import org.apache.commons.io.FileUtils;

class QuotaState {
    public long quota;
    public long size;

    public long getQuota() {
        return quota;
    }

    public long getSize() {
        return size;
    }

    public String getQuotaDescription() {
        return FileUtils.byteCountToDisplaySize(quota - size) + " free / " + FileUtils.byteCountToDisplaySize(quota);
    }
}
