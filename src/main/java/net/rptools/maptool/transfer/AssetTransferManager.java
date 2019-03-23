/*
 * This software Copyright by the RPTools.net development team, and
 * licensed under the Affero GPL Version 3 or, at your option, any later
 * version.
 *
 * MapTool Source Code is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public
 * License * along with this source Code.  If not, please visit
 * <http://www.gnu.org/licenses/> and specifically the Affero license
 * text at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool.transfer;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class AssetTransferManager {
  private Map<Serializable, AssetConsumer> consumerMap = new HashMap<Serializable, AssetConsumer>();
  private List<ConsumerListener> consumerListenerList =
      new CopyOnWriteArrayList<ConsumerListener>();
  private List<AssetProducer> producerList = new LinkedList<AssetProducer>();

  /** Clear out all existing consumers and producers */
  public synchronized void flush() {
    consumerMap.clear();
    producerList.clear();
  }

  /**
   * Add a new producer to the chunk queue. Assumes that the header has already been transferred to
   * the consumer. Producer chunks can then be retrieved via nextChunk()
   */
  public synchronized void addProducer(AssetProducer producer) {
    producerList.add(producer);
  }

  /**
   * Get the next chunk from the available producers
   *
   * @param size size of the data to retrieve
   * @throws IOException
   */
  public synchronized AssetChunk nextChunk(int size) throws IOException {
    if (producerList.size() == 0) {
      return null;
    }
    AssetProducer producer = producerList.remove(0);
    AssetChunk chunk = producer.nextChunk(size);
    if (!producer.isComplete()) {
      producerList.add(producer);
    }
    return chunk;
  }

  /**
   * Add the corresponding consumer that is expecting to receive chunks. Add a ConsumerListener to
   * know when the asset is complete
   */
  public synchronized void addConsumer(AssetConsumer consumer) {
    if (consumerMap.get(consumer.getId()) != null) {
      throw new IllegalArgumentException("Asset is already being downloaded: " + consumer.getId());
    }
    consumerMap.put(consumer.getId(), consumer);
    for (ConsumerListener listener : consumerListenerList) {
      listener.assetAdded(consumer.getId());
    }
  }

  /**
   * Update the appropriate asset. To be notified when the asset is complete add a ConsumerListener.
   * When the asset is complete it will be removed from the internal map automatically
   *
   * @throws IOException
   */
  public synchronized void update(AssetChunk chunk) throws IOException {
    AssetConsumer consumer = consumerMap.get(chunk.getId());
    if (consumer == null) {
      throw new IllegalArgumentException("Not expecting chunk: " + chunk.getId());
    }
    consumer.update(chunk);
    if (consumer.isComplete()) {
      consumerMap.remove(consumer.getId());
      for (ConsumerListener listener : consumerListenerList) {
        listener.assetComplete(consumer.getId(), consumer.getName(), consumer.getFilename());
      }
    } else {
      for (ConsumerListener listener : consumerListenerList) {
        listener.assetUpdated(consumer.getId());
      }
    }
  }

  /**
   * Get a list of current asset consumers, this is a good way to know what's going on in the system
   */
  public synchronized List<AssetConsumer> getAssetConsumers() {
    return new ArrayList<AssetConsumer>(consumerMap.values());
  }

  public void addConsumerListener(ConsumerListener listener) {
    consumerListenerList.add(listener);
  }

  public void removeConsumerListener(ConsumerListener listener) {
    consumerListenerList.remove(listener);
  }
}
