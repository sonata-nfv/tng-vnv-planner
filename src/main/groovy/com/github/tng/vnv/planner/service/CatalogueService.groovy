package com.github.tng.vnv.planner.service

import com.github.tng.vnv.planner.model.PackageMetadata

interface CatalogueService {

    def discoverAssociatedNssAndTests(PackageMetadata packageMetadata)
}