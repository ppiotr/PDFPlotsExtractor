/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package invenio.common;

/**
 *
 * @author piotr
 */
public class Pair<T, S> {
    public T first;
    public S second;
    public Pair(){
        this.first = null;
        this.second = null;
    }

    public Pair(T f, S s){
        this.first = f;
        this.second = s;
    }
}
