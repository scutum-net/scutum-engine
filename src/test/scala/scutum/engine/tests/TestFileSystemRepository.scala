package scutum.engine.tests

import java.io.File

import org.scalatest.WordSpecLike
import scutum.engine.repositories.FileSystemRepository

class TestFileSystemRepository extends WordSpecLike{
  "FileSystem repository" must {
    "Return working folder" in {
      val dir = FileSystemRepository.getRunningDirectory
      val files = FileSystemRepository.loadFilesFromRunningDirectory("jar")
      if(files.nonEmpty) {
        val name = new File(files.head).getName
        val result = FileSystemRepository.loadFileFromRunningDirectory(name)
        assert(result.nonEmpty)
      }

      assert(dir.length > 0 && files.nonEmpty)
    }
  }
}
