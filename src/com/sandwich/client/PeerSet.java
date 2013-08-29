package com.sandwich.client;

import android.annotation.SuppressLint;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public class PeerSet {
	private ConcurrentHashMap<Peer, Peer> peerSet;
	private AutoBlacklist autoBlacklist;
	
	public PeerSet(AutoBlacklist autoBlacklist)
	{
		this.peerSet = new ConcurrentHashMap<Peer, Peer>();
		this.autoBlacklist = autoBlacklist;
	}
	
	public synchronized void updatePeerSet(PeerSet peers)
	{	
		// Add a copy of each peer into our peer set
		for (Peer p : peers.peerSet.values())
		{
			updatePeer(new Peer(p));
		}
	}
	
	public void updatePeer(Peer p)
	{
		Peer oldPeer = peerSet.get(p);
		
		// If they're the same, we're done
		if (oldPeer == p)
			return;
		
		if (oldPeer != null)
		{
			// Update an existing peer
			synchronized (oldPeer) {
				if (oldPeer.timestamp.before(p.timestamp))
				{
					oldPeer.indexHash = p.indexHash;
					oldPeer.timestamp = p.timestamp;
					oldPeer.rawTimestamp = p.rawTimestamp;
					
					// Only update the state if it's not unknown
					if (p.state != Peer.STATE_UNKNOWN)
						oldPeer.state = p.state;
				}
			}
		}
		else if (!autoBlacklist.isPeerBlacklisted(p))
		{
			// Otherwise add a new one
			peerSet.put(p, p);
			p.setPeerSet(this);
		}
		else
		{
			System.out.println("Skipping blacklisted peer: "+p);
		}
	}
	
	public void updatePeer(String ip, String timestamp, long indexHash, int state) throws ParseException
	{
		Peer p = new Peer(ip, indexHash, state);
		p.updateTimestamp(timestamp);
		updatePeer(p);
	}
	
	public boolean removePeer(String ip)
	{
		Peer p = new Peer(ip, 0, 0);
		return removePeer(p);
	}
	
	public boolean removePeer(Peer p)
	{
		if ((p = peerSet.remove(p)) != null)
		{
			p.setPeerSet(null);
			return true;
		}

		return false;
	}
	
	public int getPeerListLength()
	{
		return peerSet.size();
	}
	
	public Iterator<Peer> getPeerListIterator()
	{
		return peerSet.values().iterator();
	}
	
	public class Peer implements Comparable<Peer> {
		private String ip;
		private long indexHash;
		private PeerSet peerSet;
		private String rawTimestamp;
		private Date timestamp;
		private int state;
		
		public static final int STATE_UNKNOWN = 0;
		public static final int STATE_UP_TO_DATE = 1;
		public static final int STATE_UPDATING = 2;
		public static final int STATE_UPDATE_FAILED = 3;
		public static final int STATE_BLACKLISTED = 4;
		public static final int STATE_UPDATE_FORBIDDEN = 5;
		
		public Peer(String ip, long indexHash, int state)
		{
			this.ip = ip;
			this.indexHash = indexHash;
			this.peerSet = null;
			this.state = state;
		}
		
		public Peer(Peer p)
		{
			this.ip = p.ip;
			this.indexHash = p.indexHash;
			this.peerSet = null;
			this.rawTimestamp = p.rawTimestamp;
			this.timestamp = p.timestamp;
			this.state = p.state;
		}
		
		private void setPeerSet(PeerSet peerSet)
		{
			this.peerSet = peerSet;
		}
		
		public PeerSet getPeerSet()
		{
			return peerSet;
		}
		
		@SuppressLint("SimpleDateFormat")
		public void updateTimestamp(String timestamp) throws ParseException
		{
			SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSZ");
			
			this.timestamp = parser.parse(timestamp);
			this.rawTimestamp = timestamp;
		}
		
		public boolean remove()
		{
			if (peerSet != null)
				return peerSet.removePeer(this);
			else
				return false;
		}
		
		public String getIpAddress()
		{
			return ip;
		}
		
		public long getIndexHash()
		{
			return indexHash;
		}
		
		public Date getTimestamp()
		{
			return timestamp;
		}
		
		public String getRawTimestamp()
		{
			return rawTimestamp;
		}
		
		public void updateIndexHash(long newHash)
		{
			indexHash = newHash;
		}
		
		public void setState(int state)
		{
			this.state = state;
		}
		
		public int getState()
		{
			return state;
		}
		
		@Override
		public boolean equals(Object o)
		{
			if (!(o instanceof Peer))
				return false;
			
			Peer p = (Peer)o;
			
			// The IP address should be unique
			return p.getIpAddress().equals(getIpAddress());
		}
		
		@Override
		public int hashCode()
		{
			// The IP address is the unique portion
			return getIpAddress().hashCode();
		}
		
		@Override
		public String toString()
		{
			// Start with the IP address
			String str = getIpAddress();
			
			// Append some state details
			switch (state) {
			case STATE_BLACKLISTED:
				str += " (Blacklisted)";
				break;
				
			case STATE_UPDATING:
				str += " (Index Updating)";
				break;
				
			case STATE_UPDATE_FAILED:
				str += " (Update Failed)";
				break;
				
			case STATE_UPDATE_FORBIDDEN:
				str += " (Update Rejected)";
				break;
				
			case STATE_UP_TO_DATE:
			case STATE_UNKNOWN:
			default:
				break;
			}
			
			return str;
		}

		@Override
		public int compareTo(Peer otherPeer) {
			return getIpAddress().compareTo(otherPeer.getIpAddress());
		}
	}
}
