/*
 * Copyright 2016 org.NLP4L
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.nlp4l.lucene

import org.apache.lucene.search.DocIdSetIterator

import scala.collection.immutable.BitSet
import scala.collection.SortedSet

/**
 * Trait representing DocSet. This holds Lucene Document IDs. By extending scala.collection.SortedSet, this equips all the set operations provided by Scala collection library.
 */
trait DocSet extends SortedSet[Int] {
  
  def docSet: SortedSet[Int]

  override def contains(elem: Int): Boolean = docSet.contains(elem)

  override def +(elem: Int): SortedSet[Int] = docSet + elem

  override def -(elem: Int): SortedSet[Int] = docSet - elem

  override def iterator: Iterator[Int] = docSet.iterator

  override implicit def ordering: Ordering[Int] = Ordering.Int

  override def rangeImpl(from: Option[Int], until: Option[Int]): SortedSet[Int] = docSet.rangeImpl(from, until)

  override def keysIteratorFrom(start: Int): Iterator[Int] = docSet.keysIteratorFrom(start)
}

/**
 * Factory for [[DocSet]] instances.
 */
object DocSet {
  /**
   * Create a DocSet with given DocIdSetIterator.
   * @param docIdSetItr the DocIdSetIterator instance
   * @return the doc id set
   */
  // TODO use BitDocSet if doc set size is large
  def apply(docIdSetItr: DocIdSetIterator): DocSet = new IntDocSet(docIdSetItr)

  /**
   * Convert DocSet to Set implicitly
   * @param ds the doc id set
   * @return the converted set
   */
  implicit def convert2Set(ds: DocSet): Set[Int] = ds.docSet.toSet
}

/**
 * Class implementing DocSet using scala.collection.SortedSet
 * @param docIdItr the DocIdSetIterator instance
 */
class IntDocSet(docIdItr: DocIdSetIterator) extends DocSet {
  
  val intSet = 
    if (docIdItr == null)
      SortedSet.empty[Int]
    else {
      val builder = SortedSet.newBuilder[Int]
      while (docIdItr.nextDoc() != DocIdSetIterator.NO_MORE_DOCS) {
        builder += docIdItr.docID()
      }
      builder.result()
    }

  override def docSet = intSet
  
}

/**
 * Class implementing DocSet using scala.collection.BitSet
 * @param docIdItr the DocIdSetIterator instance
 * @param initialSize the initial size of BitDocSet
 */
class BitDocSet(docIdItr: DocIdSetIterator, initialSize: Int = 64) extends DocSet{
  
  val bitSet =
    if (docIdItr == null) 
      BitSet.empty
    else {
      val builder = BitSet.newBuilder
      while (docIdItr.nextDoc() != DocIdSetIterator.NO_MORE_DOCS) {
        builder += docIdItr.docID()
      }
      builder.result()
    }

  override def docSet = bitSet

}