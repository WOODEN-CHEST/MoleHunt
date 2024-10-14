package sus.keiger.molehunt.player;

import java.util.List;

public interface IAudienceMemberHolder extends IAudienceMember
{
    List<? extends IAudienceMember> GetAudienceMembers();
}