package io.github.lounode.ae2cs.common.me.logic;

import appeng.api.networking.crafting.*;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.storage.StorageHelper;
import appeng.helpers.NonNullArrayIterator;
import com.google.common.collect.ImmutableSet;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

// 复制自AE原版类
public class MultiCraftingTracker
{
    private final int size;
    private final ICraftingRequester owner;

    private Future<ICraftingPlan>[] jobs = null;
    private ICraftingLink[] links = null;

    public MultiCraftingTracker(ICraftingRequester o, int size)
    {
        this.owner = o;
        this.size = size;
    }

    public void readFromNBT(ValueInput extra)
    {
        for (int x = 0; x < this.size; x++)
        {
            final int slot = x;
            extra.child("links-" + x)
                    .ifPresent(link -> this.setLink(slot, StorageHelper.loadCraftingLink(link, this.owner)));
        }
    }

    public void writeToNBT(ValueOutput extra)
    {
        for (int x = 0; x < this.size; x++)
        {
            final ICraftingLink link = this.getLink(x);

            if (link != null)
            {
                link.writeToNBT(extra.child("links-" + x));
            }
        }
    }

    public boolean handleCrafting(int x, AEKey what, long amount,
                                  Level level, ICraftingService cg, IActionSource mySrc)
    {
        var craftingJob = this.getJob(x);
        if (this.getLink(x) != null)
        {
            return false;
        }

        // We're already running a crafting job
        if (craftingJob != null)
        {
            try
            {
                ICraftingPlan job = null;
                if (craftingJob.isDone())
                {
                    job = craftingJob.get();
                }

                // Check if job is complete
                if (job != null)
                {
                    var result = cg.submitJob(job, this.owner, null, false, mySrc);

                    this.setJob(x, null);

                    if (result.successful())
                    {
                        this.setLink(x, result.link());

                        return true;
                    }
                }
            }
            catch (InterruptedException e)
            {
                // :P
            }
            catch (ExecutionException e)
            {
                // :P
            }
        }
        else if (this.getLink(x) == null)
        {
            this.setJob(x,
                    cg.beginCraftingCalculation(level, () -> mySrc, what, amount, CalculationStrategy.CRAFT_LESS));
        }
        return false;
    }

    public ImmutableSet<ICraftingLink> getRequestedJobs()
    {
        if (this.links == null)
        {
            return ImmutableSet.of();
        }

        return ImmutableSet.copyOf(new NonNullArrayIterator<>(this.links));
    }

    public void jobStateChange(ICraftingLink link)
    {
        if (this.links != null)
        {
            for (int x = 0; x < this.links.length; x++)
            {
                if (this.links[x] == link)
                {
                    this.setLink(x, null);
                    return;
                }
            }
        }
    }

    int getSlot(ICraftingLink link)
    {
        if (this.links != null)
        {
            for (int x = 0; x < this.links.length; x++)
            {
                if (this.links[x] == link)
                {
                    return x;
                }
            }
        }

        return -1;
    }

    void cancel()
    {
        if (this.links != null)
        {
            for (ICraftingLink l : this.links)
            {
                if (l != null)
                {
                    l.cancel();
                }
            }

            this.links = null;
        }

        if (this.jobs != null)
        {
            for (Future<ICraftingPlan> l : this.jobs)
            {
                if (l != null)
                {
                    l.cancel(true);
                }
            }

            this.jobs = null;
        }
    }

    boolean isBusy(int slot)
    {
        return this.getLink(slot) != null || this.getJob(slot) != null;
    }

    private ICraftingLink getLink(int slot)
    {
        if (this.links == null)
        {
            return null;
        }

        return this.links[slot];
    }

    private void setLink(int slot, ICraftingLink l)
    {
        if (this.links == null)
        {
            this.links = new ICraftingLink[this.size];
        }

        this.links[slot] = l;

        boolean hasStuff = false;
        for (int x = 0; x < this.links.length; x++)
        {
            final ICraftingLink g = this.links[x];

            if (g == null || g.isCanceled() || g.isDone())
            {
                this.links[x] = null;
            }
            else
            {
                hasStuff = true;
            }
        }

        if (!hasStuff)
        {
            this.links = null;
        }
    }

    private Future<ICraftingPlan> getJob(int slot)
    {
        if (this.jobs == null)
        {
            return null;
        }

        return this.jobs[slot];
    }

    private void setJob(int slot, Future<ICraftingPlan> l)
    {
        if (this.jobs == null)
        {
            this.jobs = new Future[this.size];
        }

        this.jobs[slot] = l;

        boolean hasStuff = false;

        for (Future<ICraftingPlan> job : this.jobs)
        {
            if (job != null)
            {
                hasStuff = true;
                break;
            }
        }

        if (!hasStuff)
        {
            this.jobs = null;
        }
    }
}
