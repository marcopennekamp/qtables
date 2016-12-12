package io.collap.qtables

trait ActionChecks {
  implicit class BulkActionChecker(seq: Seq[Long]) {
    private def check(kind: String): Unit = {
      val failed = seq.filter(_ <= 0)
      if (failed.nonEmpty) {
        throw new RuntimeException(s"${failed.size} $kind failed!")
      }
    }

    /**
      * Checks whether bulk insertions were successful. If at least one insertion failed, a [[RuntimeException]] is thrown.
      */
    def checkInsertions() = check("insertions")

    /**
      * Checks whether bulk updates were successful. If at least one update failed, a [[RuntimeException]] is thrown.
      */
    def checkUpdates() = check("updates")

    /**
      * Checks whether bulk deletions were successful. If at least one deletion failed, a [[RuntimeException]] is thrown.
      */
    def checkDeletions() = check("deletions")
  }

  implicit class ActionChecker(value: Long) {
    private def check(kind: String): Unit = {
      if (value <= 0) {
        throw new RuntimeException(s"$kind failed!")
      }
    }

    /**
      * Checks whether an insertion was successful. If not, a [[RuntimeException]] is thrown.
      */
    def checkInsertion() = check("Insertion")

    /**
      * Checks whether an update was successful. If not, a [[RuntimeException]] is thrown.
      */
    def checkUpdate() = check("Update")

    /**
      * Checks whether a deletion was successful. If not, a [[RuntimeException]] is thrown.
      */
    def checkDeletion() = check("Deletion")
  }
}
