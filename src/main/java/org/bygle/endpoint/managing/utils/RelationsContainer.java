package org.bygle.endpoint.managing.utils;

import java.util.List;

public class RelationsContainer {
		private long idRecord;
		private List<?> nodeList;
		byte[]  oldRdf;
		public RelationsContainer(long idRecord,List<?> nodeList) {
			this.idRecord = idRecord;
			this.nodeList = nodeList;
		}
		public RelationsContainer(long idRecord,byte[]  oldRdf) {
			this.idRecord = idRecord;
			this.oldRdf = oldRdf;
		}
		public long getIdRecord() {
			return idRecord;
		}
		public void setIdRecord(long idRecord) {
			this.idRecord = idRecord;
		}
		public List<?> getNodeList() {
			return nodeList;
		}
		public void setNodeList(List<?> nodeList) {
			this.nodeList = nodeList;
		}
		public byte[]  getOldRdf() {
			return oldRdf;
		}
		public void setOldRdf(byte[]  oldRdf) {
			this.oldRdf = oldRdf;
		}
}
