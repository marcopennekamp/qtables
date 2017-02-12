package me.pennekamp.qtables

trait TestTable[A <: HasKey] extends Table[A] {
  override type Quoted[Q] = QuillContext.Quoted[Q]
  override type EntityQuery[T] = QuillContext.EntityQuery[T]
}
