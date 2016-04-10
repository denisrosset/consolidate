package com.faacets
package consolidate

sealed trait Merged[+A] {

  def get: A

  def withPath(pathElement: String): Merged[A]

  def map[B](f: A => B): Merged[B]

  def flatMap[B](f: A => Merged[B]): Merged[B]

  def log: MLog

  def failOn(error: A => Boolean, message: String): Merged[A] =
    if (error(get)) MFail(get, log ++ MLog(Map(Nil -> message))) else this

}

case class MSame[A](a: A) extends Merged[A] {

  def get = a

  def withPath(pathElement: String) = this

  def map[B](f: A => B): Merged[B] = MSame(f(a))

  def log = MLog.empty

  def flatMap[B](f: A => Merged[B]) = f(a)

}

case class MNew[A](a: A, log: MLog) extends Merged[A] {

  def get = a

  def withPath(pathElement: String) = MNew(a, log.withPath(pathElement))

  def map[B](f: A => B): Merged[B] = MNew(f(a), log)

  def flatMap[B](f: A => Merged[B]) = f(a) match {
    case MSame(res) => MNew(res, log)
    case MNew(res, log2) => MNew(res, log ++ log2)
    case MFail(res, log2) => MFail(res, log ++ log2)
  }

}

case class MFail[A](a: A, log: MLog) extends Merged[A] {

  def get = a

  def withPath(pathElement: String) = MFail(a, log.withPath(pathElement))

  def map[B](f: A => B): Merged[B] = MFail(f(a), log)

  def flatMap[B](f: A => Merged[B]) = f(a) match {
    case MSame(res) => MFail(res, log)
    case MNew(res, log2) => MFail(res, log ++ log2)
    case MFail(res, log2) => MFail(res, log ++ log2)
  }
  
}
