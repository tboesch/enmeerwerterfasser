import AssemblyKeys._ // put this at the top of the file

assemblySettings

// your assembly settings here

mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
  {
    case PathList("org", "slf4j", _*)         => MergeStrategy.first
    case x => old(x)
  }
}